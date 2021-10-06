import React from 'react';

import {AsyncSelectOptionField} from './SelectField';
import './SelectField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import NOTES from './NOTES.mdx';

export default {
    title: 'Example/AsyncSelectOptionField',
    component: AsyncSelectOptionField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <AsyncSelectOptionField {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    value: null,
};

export const FieldDiabledState = FieldTemplate.bind({});
FieldDiabledState.args = {
    value: options[0],
    disabled: true,
};

export const FieldWithError = FieldTemplate.bind({});
FieldWithError.args = {
    value: options[0],
    hasError: true,
};

export const FieldWithInvalidValue = FieldTemplate.bind({});
FieldWithInvalidValue.args = {
    value: {value: 'marzipan', label: 'Marzipan'},
    hasError: true,
};


export const EmptyFieldWithPlaceholder = FieldTemplate.bind({});
EmptyFieldWithPlaceholder.args = {
    placeholder: "This is the placeholder",
};

export const DesignDecisions = NOTES;
