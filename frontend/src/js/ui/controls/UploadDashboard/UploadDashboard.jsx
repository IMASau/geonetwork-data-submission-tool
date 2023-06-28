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

        const uppy = new Uppy({
            id: 'uppy',
            autoProceed: true,
            debug: true,
            onBeforeUpload(files) {
                for (const [_, file] of Object.entries(files)) {
                    for (const [key, value] of Object.entries(metadata)) {
                        file.meta[key] = value;
                    }
                }

                const source = Object.values(files)[0].source;
                if (source == 'react:Dashboard') {
                    uppy.removePlugin(uppy.getPlugin('Tus'));
                } else {
                    uppy.removePlugin(uppy.getPlugin('XHRUpload'));
                }
            }
        })
            .on(
                'complete', result => {
                    const source = result.successful.concat(result.failed)[0].source;
                    console.log(source);
                    if (source == 'react:Dashboard') {
                        uppy.use(Tus, { endpoint: tusUrl });
                    } else {
                        uppy.use(XHR, { endpoint: xhrUrl, formData: true, headers: { 'X-CSRFToken': csrf } });
                    }
                }
            )
            .use(Tus, { endpoint: tusUrl })
            .use(XHR, { endpoint: xhrUrl, formData: true, headers: { 'X-CSRFToken': csrf } })
            .use(GoogleDrive, { companionUrl: companionUrl })
            .use(Url, { companionUrl: companionUrl })
            .use(ASDC, { companionUrl: companionUrl })
            .on('upload-success', onUploadSuccess);

        this.uppy = uppy;
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

