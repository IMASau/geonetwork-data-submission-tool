import React from 'react';

import {AsyncSelectField} from './AsyncSelectField';
import './AsyncSelectField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import NOTES from './NOTES.mdx';
import {InputField} from "../InputField/InputField";

export default {
    title: 'Example/AsyncSelectField',
    component: AsyncSelectField,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => (
    <div>
        <div style={{display: "flex", flexDirection: "row", fill: 1}}>
            <div style={{width: 200, padding: 5}}><AsyncSelectField disabled={args.disabled} {...args} /></div>
            <div style={{width: 200, padding: 5}}><InputField disabled={args.disabled} value={"Marzipan"}/></div>
        </div>
        <div style={{display: "flex", flexDirection: "row", fill: 1}}>
            <div style={{width: 200, padding: 5}}><InputField disabled={args.disabled} value={"Marzipan"}/></div>
        </div>
    </div>
)

const options = [
    {value: 'chocolate', label: 'Chocolate'},
    {value: 'strawberry', label: 'Strawberry'},
    {value: 'vanilla', label: 'Vanilla'}
]

const filterColors = (inputValue) => {
    return options.filter(i =>
        i.label.toLowerCase().includes(inputValue.toLowerCase())
    );
};

const promiseOptions = inputValue =>
    new Promise(resolve => {
        console.log("promiseOptions.inputValue", {inputValue})
        setTimeout(() => {
            console.log("promiseOptions.inputValue.timeout", {inputValue})
            resolve(filterColors(inputValue));
        }, 1000);
    });

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    value: null,
    loadOptions: promiseOptions,
    placeholder: "",
    disabled: false,
    hasError: false,
};

export const FieldDiabledState = FieldTemplate.bind({});
FieldDiabledState.args = {
    value: options[0],
    loadOptions: promiseOptions,
    disabled: true,
};

export const FieldWithError = FieldTemplate.bind({});
FieldWithError.args = {
    value: options[0],
    loadOptions: promiseOptions,
    hasError: true,
};

export const FieldWithInvalidValue = FieldTemplate.bind({});
FieldWithInvalidValue.args = {
    value: {value: 'marzipan', label: 'Marzipan'},
    loadOptions: promiseOptions,
    hasError: true,
};


export const EmptyFieldWithPlaceholder = FieldTemplate.bind({});
EmptyFieldWithPlaceholder.args = {
    loadOptions: promiseOptions,
    placeholder: "This is the placeholder",
};

export const DesignDecisions = NOTES;
