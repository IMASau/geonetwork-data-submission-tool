import * as React from 'react';

import {AsyncBreadcrumbAddSelectField} from './SelectField';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {options, getLabel, getBreadcrumb, getValue, loadOptions} from './storyUtils'

export default {
    title: 'SelectField/AsyncBreadcrumbAddSelectField',
    component: AsyncBreadcrumbAddSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
        onBlur: {action: 'onBlur'},
        onAdd: {action: 'onAdd'},
    }
};

const AsyncBreadcrumbAddSelectFieldTemplate = (args) => <AsyncBreadcrumbAddSelectField {...args} />;

export const Simple = AsyncBreadcrumbAddSelectFieldTemplate.bind({});
Simple.args = {
    value: null,
    loadOptions: loadOptions,
    placeholder: "Pick...",
    getValue: getValue,
    getLabel: getLabel,
    getBreadcrumb: getBreadcrumb
};
