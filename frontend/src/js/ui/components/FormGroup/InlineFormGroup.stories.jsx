import React from 'react';

import {InlineFormGroup} from './FormGroup';
import './FormGroup.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import '@blueprintjs/popover2/lib/css/blueprint-popover2.css';
import {InputField} from "../InputField/InputField";
import {DateField} from "../DateField/DateField";
import {YesNoRadioGroup} from "../YesNoRadioGroup/YesNoRadioGroup";
import NOTES from './NOTES.mdx';
import {TextareaField} from "../TextareaField/TextareaField";
import {SimpleSelectField} from "../SelectField/SelectField";

export default {
    title: 'Example/InlineFormGroup',
    component: InlineFormGroup,
    argTypes: {
        // Most are inferred from propTypes
    }
};

const Template = (args) => (
    <div>

        <InlineFormGroup {...args} label="Short label">
            <InputField disabled={args.disabled} hasError={args.hasError} />
        </InlineFormGroup>

        <InlineFormGroup {...args} label="Much much longer label">
            <DateField disabled={args.disabled}
                hasError={args.hasError}
                minDate={new Date("2021-01-01")}
                maxDate={new Date("2021-12-31")} />
        </InlineFormGroup>

        <InlineFormGroup {...args} >
            <YesNoRadioGroup label="Has this been published before?" disabled={args.disabled} hasError={args.hasError} />
        </InlineFormGroup>

        <InlineFormGroup {...args}>
            <TextareaField />
        </InlineFormGroup>


        <InlineFormGroup {...args}>
            <SimpleSelectField options={[]} disabled={args.disabled} hasError={args.hasError} />
        </InlineFormGroup>

        <InlineFormGroup {...args}>
            <YesNoRadioGroup
                label="Has this been published before?"
                disabled={args.disabled}
                hasError={args.hasError} />
            <DateField disabled={args.disabled}
                hasError={args.hasError}
                minDate={new Date("2021-01-01")}
                maxDate={new Date("2021-12-31")} />
        </InlineFormGroup>

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
