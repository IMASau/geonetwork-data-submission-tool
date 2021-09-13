import React from 'react';

import {TextareaField} from './TextareaField';
import './TextareaField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import NOTES from './NOTES.mdx';

export default {
    title: 'Example/TextareaField',
    component: TextareaField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <TextareaField {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    value: "Hello",
    placeholder: "",
    disabled: false,
    hasError: false,
};

export const FieldDiabledState = FieldTemplate.bind({});
FieldDiabledState.args = {
    value: "Hello",
    placeholder: "",
    disabled: true,
    hasError: false,
};

export const FieldWithError = FieldTemplate.bind({});
FieldWithError.args = {
    value: "Hello",
    placeholder: "",
    disabled: false,
    hasError: true,
};


export const EmptyFieldWithPlaceholder = FieldTemplate.bind({});
EmptyFieldWithPlaceholder.args = {
    value: "",
    placeholder: "This is the placeholder",
    disabled: false,
    hasError: false,
};


export const ChangeField = (args) => {
    const [value, setValue] = React.useState(args.value);
    return <FieldTemplate {...args} value={value} onChange={(v) => setValue(v)}/>;
};
ChangeField.args = {
};

export const DesignDecisions = NOTES;
