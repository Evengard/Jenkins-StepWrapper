package ru.trioptimum.jenkins.stepwrapper;

import edu.umd.cs.findbugs.annotations.NonNull;
import groovy.lang.Binding;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.cps.GroovySourceFileAllowlist;

@Extension
public class StepWrapperDSL extends GlobalVariable {

    @NonNull
    @Override
    public String getName() {
        return "wrapStep";
    }

    @NonNull
    @Override
    public Object getValue(@NonNull CpsScript script) throws Exception {
        Binding binding = script.getBinding();
        Object wrapStep;
        if (binding.hasVariable(getName())) {
            wrapStep = binding.getVariable(getName());
        } else {
            wrapStep = script.getClass().getClassLoader().loadClass("ru.trioptimum.jenkins.stepwrapper.WrapStep").getConstructor(CpsScript.class).newInstance(script);
            binding.setVariable(getName(), wrapStep);
        }
        return wrapStep;
    }

    @Extension
    public static class StepWrapperDSLAllowlist extends GroovySourceFileAllowlist {
        private final String scriptUrl = StepWrapperDSL.class.getResource("WrapStep.groovy").toString();

        @Override
        public boolean isAllowed(String groovySourceFileUrl) {
            return groovySourceFileUrl.equals(scriptUrl);
        }
    }
}
