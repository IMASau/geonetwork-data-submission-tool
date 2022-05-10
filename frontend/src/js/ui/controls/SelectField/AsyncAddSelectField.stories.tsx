import * as React from 'react';

import {AsyncAddSelectField} from './SelectField';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {options, getLabel, getBreadcrumb, getValue, loadOptions} from './storyUtils'

export default {
    title: 'SelectField/AsyncAddSelectField',
    component: AsyncAddSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
        onBlur: {action: 'onBlur'},
        onAdd: {action: 'onAdd'},
    }
};

const AsyncAddSelectFieldTemplate = (args) => <AsyncAddSelectField {...args} />;

export const Simple = AsyncAddSelectFieldTemplate.bind({});
Simple.args = {
    value: null,
    loadOptions: loadOptions,
    placeholder: "Pick...",
    getValue: getValue,
    getLabel: getLabel
};
