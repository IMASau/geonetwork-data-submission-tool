import React from 'react';

import {BoxMapFill} from './BoxMap';
import {EditDialog} from '../EditDialog/EditDialog';
import './boxmap.css';
import 'leaflet/dist/leaflet.css';
import 'leaflet-draw/dist/leaflet.draw.css';

export default {
    title: 'Example/BoxMapFill',
    component: BoxMapFill,
    argTypes: {
        onChange: {action: 'onChange'},
    },
};

const Template = (args) => <BoxMapFill {...args} />;

export const EmptyMap = Template.bind({});
EmptyMap.args = {
    elements: []
};

export const MapWithValues = Template.bind({});
MapWithValues.args = {
    elements: [
        {
            "northBoundLatitude": 39,
            "southBoundLatitude": 39,
            "eastBoundLongitude": 144.272461,
            "westBoundLongitude": 144.272461
        },
        {
            "northBoundLatitude": -41.821091,
            "southBoundLatitude": -45.381173,
            "eastBoundLongitude": 150.448172,
            "westBoundLongitude": 146.587417
        }]
};


export const BoxMapDialogInteraction = (args) => (
    <div style={{height: 300}}>
        <BoxMapFill elements={[]} onChange={e=>null} />
        <EditDialog
            isOpen={true}
            title="Dialog should fully cover map"
            onClose={() => null}
            onClear={() => null}
            onSave={() => null}
            canSave={false}
         >
             Do I cover the map?
        </EditDialog>
    </div>
);
EmptyMap.args = {
    elements: []
};