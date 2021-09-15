import React from 'react';

import {SelectionList} from './SelectionList';
import './SelectionList.css';
import NOTES from './NOTES.mdx';

export default {
    title: 'Example/SelectionList',
    component: SelectionList,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const exampleItems = [
    {value: 'chocolate', label: 'Chocolate'},
    {value: 'strawberry', label: 'Strawberry'},
    {value: 'vanilla', label: 'Vanilla'}
]

const FieldTemplate = (args) => <SelectionList {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    items: exampleItems,
};

const reorder = (list, startIndex, endIndex) => {
    const result = Array.from(list);
    const [removed] = result.splice(startIndex, 1);
    result.splice(endIndex, 0, removed);

    return result;
};

export const ReorderingExample = (args) => {
    const [items, setItems] = React.useState(exampleItems);
    return <FieldTemplate {...args}
                          items={items}
                          onReorder={
                              (startIndex, endIndex) =>
                                  setItems(reorder(items, startIndex, endIndex))}/>;
};
ReorderingExample.args = {};


export const DesignDecisions = NOTES;
