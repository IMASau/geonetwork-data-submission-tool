import React from 'react';

import {SelectionList} from './SelectionList';
import './SelectionList.css';

export default {
    title: 'Example/SelectionList',
    component: SelectionList,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const exampleValues = [
    { value: 'chocolate', label: 'Chocolate' },
    { value: 'strawberry', label: 'Strawberry' },
    { value: 'vanilla', label: 'Vanilla' }
]

const FieldTemplate = (args) => <SelectionList {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    values: exampleValues,
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;
