import React from 'react';

import {RecordAddField} from './RecordAddField';
import './RecordAddField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';

export default {
    title: 'Example/RecordAddField',
    component: RecordAddField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const columns = [{
    placeholder: "Title",
    flex: 1,
    maxLength: 5
}, {
    placeholder: "Name",
    flex: 2
}]

const FieldTemplate = (args) => <RecordAddField {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    columns: columns,
    disabled: false,
    buttonText: "Add",
};

export const FieldDisabledState = FieldTemplate.bind({});
FieldDisabledState.args = {
    columns: columns,
    disabled: true,
    buttonText: "Add",
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;
