package ru.trioptimum.jenkins.stepwrapper;

import com.cloudbees.groovy.cps.NonCPS;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import groovy.lang.Closure;
import hudson.Extension;
import org.jenkinsci.plugins.structs.describable.DescribableModel;
import org.jenkinsci.plugins.structs.describable.UninstantiatedDescribable;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StepWrapperStep extends Step {
    private final Closure<StepExecution> closure;

    @DataBoundConstructor
    public StepWrapperStep(Step delegate) {
        this.closure = StepToClosure(delegate);
    }

    @NonCPS
    private static Closure<StepExecution> StepToClosure(Step delegate) {
        return new Closure<StepExecution>(null) {
            @Override
            @NonCPS
            public StepExecution call(Object... args) {
                var ctx = (StepContext)args[0];
                try {
                    return delegate.start(ctx);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public StepWrapperStep(Closure<StepExecution> closure) { this.closure = closure; }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return closure.call(context);
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {
        @Override public String getFunctionName() {
            return "stepWrapper";
        }

        @NonNull
        @Override public String getDisplayName() {
            return "A step calling another step";
        }

        @Override
        public boolean isMetaStep() {
            return true;
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }


        /*
        public Collection<? extends Descriptor<?>> getApplicableDescriptors() {
            // Jenkins.instance.getDescriptorList(SimpleBuildStep) is empty, presumably because that itself is not a Describable.
            List<Descriptor<?>> r = new ArrayList<>();
            populate(r, Builder.class);
            populate(r, Publisher.class);
            return r;
        }*/

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            Set<Class<?>> context = new HashSet<>();
            //Collections.addAll(context, Step.class, StepExecution.class);
            return context;
        }

        @Override
        public String argumentsToString(Map<String, Object> namedArgs) {
            Map<String, Object> delegateArguments = delegateArguments(namedArgs.get("delegate"));
            return delegateArguments != null ? super.argumentsToString(delegateArguments) : null;
        }

        @SuppressWarnings("unchecked")
        @CheckForNull
        static Map<String, Object> delegateArguments(@CheckForNull Object delegate) {
            if (delegate instanceof UninstantiatedDescribable) {
                // TODO JENKINS-45101 getStepArgumentsAsString does not resolve its arguments
                // thus delegate.model == null and we cannot inspect DescribableModel.soleRequiredParameter
                // thus for, e.g., `junit testResults: '*.xml', keepLongStdio: true` we will get null
                return new HashMap<>(((UninstantiatedDescribable) delegate).getArguments());
            } else if (delegate instanceof Map) {
                Map<String, Object> r = new HashMap<>((Map<String, Object>) delegate);
                r.remove(DescribableModel.CLAZZ);
                return r;
            } else {
                return null;
            }
        }
    }
}
