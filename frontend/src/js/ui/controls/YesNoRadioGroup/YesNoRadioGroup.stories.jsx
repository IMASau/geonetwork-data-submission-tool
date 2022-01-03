import React from 'react';

import {YesNoRadioGroup} from './YesNoRadioGroup';
import './YesNoRadioGroup.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';


export default {
    title: 'Example/YesNoRadioGroup',
    component: YesNoRadioGroup,
    argTypes: {
        // Most are inferred from propTypes
        onChange: {action: 'onChange'},
    }
};

const FieldTemplate = (args) => <YesNoRadioGroup {...args} />;

export const NotSet = FieldTemplate.bind({});
NotSet.args = {
    label: "Is it yes or no?",
    disabled: false,
    hasError: false,
};

export const Set = FieldTemplate.bind({});
Set.args = {
    label: "Is it yes or no?",
    value: true,
    disabled: false,
    hasError: false,
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;
