import React from 'react';

import { DateField } from './DateField';
import './DateField.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';

export default {
  title: 'Example/DateField',
  component: DateField,
  argTypes: {
      label: { control: 'text', defaultValue: 'Start date' },
      required: { control: 'boolean', defaultValue: true },
      helperText: { control: 'text' },
      value: { control: 'date' },
      disabled: { control: 'boolean', defaultValue: false },
      onChange: { action: 'onChange' },
      hasError: { control: 'boolean', defaultValue: false },
      minDate: { control: 'date' },
      maxDate: { control: 'date' },
  }
};

const Wrapper = (args) => {
  const minDate = args.minDate ? new Date(args.minDate) : null;
  const maxDate = args.maxDate ? new Date(args.maxDate) : null;
  const value = args.value ? new Date(args.value) : null;

  return <DateField {...args} value={value} minDate={minDate} maxDate={maxDate} />;
};

const Template = (args) => <Wrapper {...args} />;

export const EmptyDateField = Template.bind({});
EmptyDateField.args = {
  helperText: "This one is not set",
  minDate: 946645200000,
  maxDate: 1893416400000,
};

export const PresetDateField = Template.bind({});
PresetDateField.args = {
  value: 1631628000000,
  minDate: 946645200000,
  maxDate: 1893416400000,
  helperText: "This one is preset"
};

export const ChangeDateField = (args) => {
  var [value, setValue] = React.useState(args.value);
  return <Wrapper {...args} value={value} onChange={(v,t) => setValue(v)} />;
};
ChangeDateField.args = {
  helperText: "This one can update",
  minDate: 946645200000,
  maxDate: 1893416400000,
};
