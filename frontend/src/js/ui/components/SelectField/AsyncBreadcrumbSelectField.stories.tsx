import * as React from 'react';

import {AsyncBreadcrumbSelectField} from './SelectField';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {options, getLabel, getBreadcrumb, getValue, loadOptions} from './storyUtils'

export default {
    title: 'SelectField/AsyncBreadcrumbSelectField',
    component: AsyncBreadcrumbSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const AsyncBreadcrumbSelectFieldTemplate = (args) => <AsyncBreadcrumbSelectField {...args} />;

export const Simple = AsyncBreadcrumbSelectFieldTemplate.bind({});
Simple.args = {
    value: null,
    loadOptions: loadOptions,
    placeholder: "Pick...",
    getValue: getValue,
    getLabel: getLabel,
    getBreadcrumb: getBreadcrumb
};
