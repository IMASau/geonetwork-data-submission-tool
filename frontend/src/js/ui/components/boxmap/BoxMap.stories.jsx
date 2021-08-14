import React from 'react';

import { BoxMap } from './BoxMap';
import './boxmap.css';
import 'leaflet/dist/leaflet.css';
import 'leaflet-draw/dist/leaflet.draw.css';

export default {
  title: 'Example/BoxMap',
  component: BoxMap,
  argTypes: {
    boxes: { control: 'object', defaultValue: [] },
    disabled: { control: 'boolean' },
    onChange: { action: 'onChange' },
    tickId: { control: 'number', defaultValue: 0}
  },
};

const Template = (args) => <BoxMap {...args} />;

export const Primary = Template.bind({});
Primary.args = {
};

export const Secondary = Template.bind({});
Secondary.args = {
};

export const Large = Template.bind({});
Large.args = {
};

export const Small = Template.bind({});
Small.args = {
};
