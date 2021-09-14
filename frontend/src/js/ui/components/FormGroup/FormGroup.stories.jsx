import React from 'react';

import {FormGroup} from './FormGroup';
import './FormGroup.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import '@blueprintjs/popover2/lib/css/blueprint-popover2.css';
import {InputField} from "../InputField/InputField";
import {DateInput} from "../DateField/DateInput";
import {YesNoRadioGroup} from "../YesNoRadioGroup/YesNoRadioGroup";
import NOTES from './NOTES.mdx';
import {TextareaField} from "../TextareaField/TextareaField";

export default {
    title: 'Example/FormGroup',
    component: FormGroup,
    argTypes: {
        // Most are inferred from propTypes
    }
};

const Template = (args) => (
    <div>
        <FormGroup {...args} >
            <InputField disabled={args.disabled} hasError={args.hasError}/>
        </FormGroup>
        <br/>
        <br/>
        <FormGroup {...args} >
            <DateInput disabled={args.disabled}
                       hasError={args.hasError}
                       minDate={new Date("2021-01-01")}
                       maxDate={new Date("2021-12-31")}/>
        </FormGroup>
        <br/>
        <br/>
        <FormGroup {...args} >
            <YesNoRadioGroup label="Has this been published before?" disabled={args.disabled} hasError={args.hasError}/>
        </FormGroup>
        <br/>
        <br/>
        <FormGroup {...args} >
            <TextareaField />
        </FormGroup>
        <br/>
        <br/>
        <FormGroup {...args} >
            <YesNoRadioGroup label="Has this been published before?" disabled={args.disabled} hasError={args.hasError}/>
            <DateInput disabled={args.disabled}
                       hasError={args.hasError}
                       minDate={new Date("2021-01-01")}
                       maxDate={new Date("2021-12-31")}/>
        </FormGroup>
    </div>
);
export const Simple = Template.bind({});
Simple.args = {
    label: "A label",
};

export const ErrorHighlighting = Template.bind({});
ErrorHighlighting.args = {
    label: "Some questions",
    required: false,
    helperText: "This is the helper text",
    toolTip: "This is the helper text",
    disabled: false,
    hasError: true
};

export const DesignDecisions = NOTES;
