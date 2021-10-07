import * as React from 'react';

import {TableSelectField} from './SelectField';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import {options, getLabel, getBreadcrumb, getValue} from './storyUtils'

export default {
    title: 'SelectField/TableSelectField',
    component: TableSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const TableSelectFieldTemplate = (args) => <TableSelectField {...args} />;

export const Simple = TableSelectFieldTemplate.bind({});
Simple.args = {
    value: null,
    options: options,
    placeholder: "Pick...",
    getValue: getValue,
    columns: [
        {flex: 1, getLabel: getLabel},
        {flex: 2, getLabel: getLabel},
        {flex: 3, getLabel: getLabel}
    ]
};
