import React from 'react';

import { BoxMap } from './BoxMap';
import './boxmap.css';
import 'leaflet/dist/leaflet.css';
import 'leaflet-draw/dist/leaflet.draw.css';

export default {
  title: 'Example/BoxMap',
  component: BoxMap,
  argTypes: {
    elements: { control: 'object', defaultValue: [] },
    disabled: { control: 'boolean' },
    onChange: { action: 'onChange' },
    tickId: { control: 'number', defaultValue: 0 },
    mapWidth: { control: 'number', defaultValue: 600 }
  },
};

const Template = (args) => <BoxMap {...args} />;

export const Primary = Template.bind({});
Primary.args = {
};

export const Secondary = Template.bind({});
Secondary.args = {
  elements: [
    { "northBoundLatitude": 39, "southBoundLatitude": 39, "eastBoundLongitude": 144.272461, "westBoundLongitude": 144.272461 },
    { "northBoundLatitude": -41.821091, "southBoundLatitude": -45.381173, "eastBoundLongitude": 150.448172, "westBoundLongitude": 146.587417 }]
};

export const Large = Template.bind({});
Large.args = {
};

export const Small = Template.bind({});
Small.args = {
};
