import React, { useCallback } from 'react'
import PropTypes from 'prop-types';
import { useDropzone } from 'react-dropzone'


export function Dropzone({ disabled, onDropFile, maxSize }) {
    const onDrop = useCallback((acceptedFiles, rejectedFiles) => {
        // Do something with the files
        onDropFile({ acceptedFiles, rejectedFiles })
    }, [])
    const {getRootProps, getInputProps, isDragActive} = useDropzone({onDrop, disabled, maxSize});

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
    maxSize: PropTypes.number,
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
