import React from 'react'
import Uppy from '@uppy/core'
import { Dashboard } from '@uppy/react'
import Tus from '@uppy/tus'
import GoogleDrive from '@uppy/google-drive'
import Url from '@uppy/url';
import { ASDC } from "@tern/asdcprovider";
import XHR from '@uppy/xhr-upload';

export default class UploadDashboard extends React.Component {
    constructor(props) {
        super(props);
        const { tusUrl, xhrUrl, companionUrl, csrf, onUploadSuccess, metadata } = props;

        this.uppy = new Uppy({
            id: 'uppy',
            autoProceed: true,
            debug: true,
            onBeforeUpload(files) {
                for (const [_, file] of Object.entries(files)) {
                    for (const [key, value] of Object.entries(metadata)) {
                        file.meta[key] = value;
                    }
                }
            }
        })
            .use(Tus, { endpoint: tusUrl })
            .use(XHR, { endpoint: xhrUrl, formData: true, headers: {'X-CSRFToken': csrf} })
            .use(GoogleDrive, { companionUrl: companionUrl })
            .use(Url, { companionUrl: companionUrl })
            .use(ASDC, { companionUrl: companionUrl })
            .on('upload-success', onUploadSuccess);
    }

    componentWillUnmount() {
        this.uppy.close({ reason: 'unmount' })
    }

    render() {
        return (
            <Dashboard
                uppy={this.uppy}
                plugins={['GoogleDrive', 'Url', 'ASDC']}
                width='100%'
                metaFields={[
                    { id: 'name', name: 'Name', placeholder: 'File name' }
                ]}
            />
        );
    }
}

