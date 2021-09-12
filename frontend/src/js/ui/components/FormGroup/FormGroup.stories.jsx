import React from 'react';

import {FormGroup} from './FormGroup';
import './FormGroup.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import '@blueprintjs/popover2/lib/css/blueprint-popover2.css';
import {InputField} from "../InputField/InputField";
import {DateInput} from "../DateField/DateInput";
import {YesNoRadioGroup} from "../YesNoRadioGroup/YesNoRadioGroup";

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
                       maxDate={new Date("2021-12-31")} />
        </FormGroup>
        <br/>
        <br/>
        <FormGroup {...args} >
            <YesNoRadioGroup label="Has this been published before?" disabled={args.disabled} hasError={args.hasError}/>
        </FormGroup>
        <br/>
        <br/>
        <FormGroup {...args} >
            <YesNoRadioGroup label="Has this been published before?" disabled={args.disabled} hasError={args.hasError}/>
            <DateInput disabled={args.disabled}
                       hasError={args.hasError}
                       minDate={new Date("2021-01-01")}
                       maxDate={new Date("2021-12-31")} />
            <YesNoRadioGroup disabled={args.disabled} hasError={args.hasError}/>
        </FormGroup>
    </div>
);
export const Simple = Template.bind({});
Simple.args = {
    label: "Some questions",
    helperText: "This is the helper text",
};

export const ErrorHighlighting = Template.bind({});
ErrorHighlighting.args = {
    label: "Some questions",
    required: false,
    inline: false,
    helperText: "This is the helper text",
    toolTip: "This is the helper text",
    disabled: false,
    hasError: true
};

export const InlineLayout = Template.bind({});
InlineLayout.args = {
    label: "Some questions",
    required: false,
    inline: true,
    helperText: "This is the helper text",
    toolTip: "This is the helper text",
    disabled: false,
    hasError: false
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;
