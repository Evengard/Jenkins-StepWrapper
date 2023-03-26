package ru.trioptimum.jenkins.stepwrapper

import com.cloudbees.groovy.cps.NonCPS
import org.jenkinsci.plugins.structs.describable.DescribableModel
import org.jenkinsci.plugins.workflow.cps.CpsScript
import org.jenkinsci.plugins.workflow.steps.Step
import org.jenkinsci.plugins.workflow.steps.StepExecution

class WrapStep {
    private CpsScript script

    public WrapStep(CpsScript script) {
        this.script = script
    }

    @NonCPS
    private Step construct(String className, Map params) {
        Class<? extends Step> target = this.class.getClassLoader().loadClass(className)
        DescribableModel<? extends Step> stepModel = DescribableModel.of(target)
        if (stepModel.getParameters().size() == 0) {
            return stepModel.instantiate([:], null)
        }
        def param = stepModel.getSoleRequiredParameter()
        if (param != null && params.size() == 1) {
            return stepModel.instantiate([(param.getName()): params.values().first()], null)
        }
        return stepModel.instantiate(params ?: [:], null)
    }

    void call(String className, Object param) {
        call(className, [param: param])
    }

    void call(String className, Object param, Closure body) {
        call(className, [param: param]) {
            body()
        }
    }

    void call(String className, Map params) {
        script.stepWrapper(construct(className, params)) { }
    }

    void call(String className, Map params, Closure body) {
        script.stepWrapper(construct(className, params)) {
            body()
        }
    }

    void call(Closure<StepExecution> closure) {
        script.stepWrapper(closure) { }
    }

    void call(Closure<StepExecution> closure, Closure body) {
        script.stepWrapper(closure) {
            body()
        }
    }
}