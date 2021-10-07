import React from 'react';

import {EditDialog} from './EditDialog';
import '@blueprintjs/core/lib/css/blueprint.css';
import './EditDialog.css';

export default {
    title: 'Example/EditDialog',
    component: EditDialog,
    argTypes: {
        onOpen: {action: 'onOpen'},
        onClose: {action: 'onClose'},
    }
};

const FieldTemplate = (args) => <EditDialog {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    body: <div>Do you want to build a snowman?</div>
};

export const DoubleDialog = FieldTemplate.bind({});
DoubleDialog.args = {
    title: "First dialog",
    body: <EditDialog title="Second dialog"
                      body="Another dialog!"
                      onClear={()=>{}}
                      onSave={()=>{}}/>
};
