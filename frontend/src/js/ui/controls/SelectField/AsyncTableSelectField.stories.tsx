import * as React from 'react';

import {AsyncTableSelectField} from './SelectField';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {options, getLabel, getBreadcrumb, getValue, loadOptions} from './storyUtils'

export default {
    title: 'SelectField/AsyncTableSelectField',
    component: AsyncTableSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
        onBlur: {action: 'onBlur'},
    }
};

const AsyncTableSelectFieldTemplate = (args) => <AsyncTableSelectField {...args} />;

export const Simple = AsyncTableSelectFieldTemplate.bind({});
Simple.args = {
    value: null,
    loadOptions: loadOptions,
    placeholder: "Pick...",
    getValue: getValue,
    getLabel: getLabel,
    columns: [
        {flex: 1, getLabel: getLabel},
        {flex: 2, getLabel: getLabel},
        {flex: 3, getLabel: getLabel}
    ]
};
