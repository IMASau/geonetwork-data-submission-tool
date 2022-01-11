import React from 'react';

import {Modal} from './EditDialog';
import '@blueprintjs/core/lib/css/blueprint.css';
import './EditDialog.css';
import {Button} from '@blueprintjs/core';

export default {
    title: 'Example/Modal',
    component: Modal,
    argTypes: {
        onSave: {action: 'onSave'},
        onCancel: {action: 'onCancel'},
        onDismiss: {action: 'onDismiss'},
    }
};

const FieldTemplate = (args) => <Modal {...args} />;

export const SimpleField = FieldTemplate.bind({});
SimpleField.args = {
    isOpen: true,
    modalHeader: "This is the heading",
    modalBody: "This is the body",
};

export const SmallModal = FieldTemplate.bind({});
SmallModal.args = {
    isOpen: true,
    modalHeader: "This is the heading",
    modalBody: "This is the body",
    dialogClass: "modal-sm"
};
