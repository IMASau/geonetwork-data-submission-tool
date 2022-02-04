import * as React from 'react';

import {AsyncSimpleSelectField} from './SelectField';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {options, getLabel, getBreadcrumb, getValue, loadOptions} from './storyUtils'

export default {
    title: 'SelectField/AsyncSimpleSelectField',
    component: AsyncSimpleSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
        onBlur: {action: 'onBlur'},
    }
};

const AsyncSimpleSelectFieldTemplate = (args) => <AsyncSimpleSelectField {...args} />;

export const Simple = AsyncSimpleSelectFieldTemplate.bind({});
Simple.args = {
    value: null,
    loadOptions: loadOptions,
    placeholder: "Pick...",
    getValue: getValue,
    getLabel: getLabel
};
