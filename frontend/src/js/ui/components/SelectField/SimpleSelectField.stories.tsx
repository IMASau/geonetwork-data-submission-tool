import * as React from 'react';

import {SimpleSelectField} from './SelectField';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {options, getLabel, getBreadcrumb, getValue} from './storyUtils'

export default {
    title: 'SelectField/SimpleSelectField',
    component: SimpleSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const SimpleSelectFieldTemplate = (args) => <SimpleSelectField {...args} />;

export const Simple = SimpleSelectFieldTemplate.bind({});
Simple.args = {
    value: null,
    options: options,
    placeholder: "Pick...",
    getValue: getValue,
    getLabel: getLabel
};
