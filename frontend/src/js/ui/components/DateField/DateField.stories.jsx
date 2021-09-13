import React from 'react';

import {DateField} from './DateField';
import './DateField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';

export default {
  title: 'Example/DateField',
    component: DateField,
    argTypes: {
    // Most are inferred from DateField.propTypes
        value: {control: 'date'},
        minDate: {control: 'date'},
        maxDate: {control: 'date'},
        onChange: {action: 'onChange'},
    }
};

// Wrapper helps because
const Wrapper = (args) => {
    const minDate = args.minDate ? new Date(args.minDate) : null;
    const maxDate = args.maxDate ? new Date(args.maxDate) : null;
    const value = args.value ? new Date(args.value) : null;

    return <DateField {...args} value={value} minDate={minDate} maxDate={maxDate}/>;
};

const Template = (args) => <Wrapper {...args} />;

export const EmptyDateField = Template.bind({});
EmptyDateField.args = {
    disabled: false,
    hasError: false,
    minDate: 947422800000,
    maxDate: 948732400000,
};

export const PresetDateField = Template.bind({});
PresetDateField.args = {
    value: 1580515200000,
    disabled: false,
    hasError: false,
    minDate: 946645200000,
    maxDate: 2524568400000,
};

export const OutOfRangeDateField = Template.bind({});
OutOfRangeDateField.args = {
    value: 2524568400000,
    disabled: false,
    hasError: false,
    minDate: 946645200000,
    maxDate: 1580515200000,
};


export const ChangeDateField = (args) => {
    const [value, setValue] = React.useState(args.value);
    return <Wrapper {...args} value={value} onChange={(v, t) => setValue(v)}/>;
};
ChangeDateField.args = {
    value: 946990800000,
    disabled: false,
    hasError: false,
    minDate: 946990800000,
    maxDate: 1583413200000,
};

import NOTES from './NOTES.mdx';
export const DesignDecisions = NOTES;
