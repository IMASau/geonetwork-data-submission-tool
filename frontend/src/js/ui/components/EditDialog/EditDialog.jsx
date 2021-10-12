import * as React from 'react';
import PropTypes from 'prop-types';
import {Button, Classes, Dialog, Overlay, Intent} from "@blueprintjs/core";

EditDialog.propTypes = {
    title: PropTypes.string.isRequired,
    children: PropTypes.node.isRequired,
    onClose: PropTypes.func.isRequired,
    onClear: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    canSave: PropTypes.bool,
}

export function EditDialog({title, children, isOpen, onClose, onClear, onSave, canSave}) {

    return (
        <Dialog
            icon="info-sign"
            onClose={onClose}
            title={title}
            canEscapeKeyClose={false}
            canOutsideClickClose={false}
            isOpen={isOpen}
            usePortal={true}
            backdropClassName="EditDialogBackdrop"
            className="EditDialogDialog"
        >
            <div className={"EditDialogBody "+Classes.DIALOG_BODY}>
                {children}
            </div>
            <div className={Classes.DIALOG_FOOTER}>
                <div className={Classes.DIALOG_FOOTER_ACTIONS}>
                    <Button onClick={onClear}>Clear</Button>
                    <Button onClick={onSave} disabled={!canSave} intent={Intent.PRIMARY}>Save</Button>
                </div>
            </div>
        </Dialog>
    )
}


EditOverlay.propTypes = {
    isOpen: PropTypes.bool.isRequired,
    title: PropTypes.string.isRequired,
    body: PropTypes.elementType.isRequired,
    onClose: PropTypes.func.isRequired,
    onClear: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    canSave: PropTypes.bool,
}

export function EditOverlay({title, body, onClear, onSave, canSave}) {
    const [isOpen, setOpen] = React.useState(false);

    function open() {
        setOpen(true)
    }

    function close() {
        setOpen(false)
    }

    function clear() {
        close();
        onClear()
    }

    function save() {
        close();
        onSave()
    }

    return (
        <div>
            <Button onClick={open}>Show Overlay</Button>
            <Overlay
                icon="info-sign"
                onClose={close}
                title={title}
                canEscapeKeyClose={false}
                canOutsideClickClose={false}
                isOpen={isOpen}
                usePortal={true}
                backdropClassName="EditOverlayBackdrop"
            >
                <div className="EditOverlayDialog">
                    <div className={"EditOverlayBody "+Classes.DIALOG_BODY}>
                        {body}
                    </div>
                    <div className={"EditOverlayFooter " +Classes.DIALOG_FOOTER}>
                        <div className={Classes.DIALOG_FOOTER_ACTIONS}>
                            <Button onClick={clear}>Clear</Button>
                            <Button onClick={save} disabled={canSave} intent={Intent.PRIMARY}>Save</Button>
                        </div>
                    </div>
                </div>
            </Overlay>
        </div>
    )
}