import * as React from 'react';

import {BreadcrumbSelectField} from './SelectField';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {options, getLabel, getBreadcrumb, getValue} from './storyUtils'

export default {
    title: 'SelectField/BreadcrumbSelectField',
    component: BreadcrumbSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const BreadcrumbSelectFieldTemplate = (args) => <BreadcrumbSelectField {...args} />;

export const Simple = BreadcrumbSelectFieldTemplate.bind({});
Simple.args = {
    value: null,
    options: options,
    placeholder: "Pick...",
    getValue: getValue,
    getLabel: getLabel,
    getBreadcrumb: getBreadcrumb
};
