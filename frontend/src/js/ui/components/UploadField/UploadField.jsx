import React, { useCallback } from 'react'
import PropTypes from 'prop-types';
import { useDropzone } from 'react-dropzone'


export function Dropzone({ disabled, onDropFile }) {
    const onDrop = useCallback(acceptedFiles => {
        // Do something with the files
        onDropFile({ acceptedFiles })
    }, [])
    const {getRootProps, getInputProps, isDragActive} = useDropzone({onDrop, disabled});

    return (
        <div>
            <div {...getRootProps({ className: 'dropzone' })}>
                <input {...getInputProps()} />
                {
                    isDragActive ?
                        <p>Drop the files here ...</p> :
                        <p>Drag 'n' drop some files here, or click to select files</p>
                }
                </div>
        </div>
    )
}

Dropzone.propTypes = {
    disabled: PropTypes.bool,
    onDropFile: PropTypes.func,
}

export function UploadField({ disabled, onDropFile }) {
    return (
        <Dropzone 
            disabled={disabled} 
            onDropFile={onDropFile} />
    );
}

UploadField.propTypes = {
    disabled: PropTypes.bool,
    onDropFile: PropTypes.func,
}
