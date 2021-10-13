import React from 'react';

import {EditOverlay} from './EditDialog';
import '@blueprintjs/core/lib/css/blueprint.css';
import './EditDialog.css';

export default {
    title: 'Example/EditOverlay',
    component: EditOverlay,
    argTypes: {
        onOpen: {action: 'onOpen'},
        onClose: {action: 'onClose'},
    }
};

const FieldTemplate = (args) => <EditOverlay {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    body: <div>Do you want to build a snowman?</div>
};

export const VeryLongOverlay = FieldTemplate.bind({});
VeryLongOverlay.args = {
    body: Array(30).fill().map(x => <div>{x}. roar</div>)
};

export const DoubleOverlay = FieldTemplate.bind({});
DoubleOverlay.args = {
    title: "First Overlay",
    body: <EditOverlay title="Second Overlay"
                       body="Another Overlay!"
                       onClear={() => {
                       }}
                       onSave={() => {
                       }}/>
};
