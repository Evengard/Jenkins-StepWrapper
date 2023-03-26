package ru.trioptimum.jenkins.stepwrapper

f = namespace(lib.FormTagLib)
f.entry(field: 'variables', title: _('delegate')) {
    f.textarea(value: instance == null ? '' : instance.class.getCanonicalName())
}
