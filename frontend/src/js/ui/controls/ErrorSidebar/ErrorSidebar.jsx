import React from 'react';
import PropTypes from 'prop-types';

function ErrorSidebarListItem({label}) {
    return (
        <li className="ErrorSidebarListItem">
            <span className="ErrorSidebarLabel">{label}</span>
        </li>
    )
}

export function ErrorSidebar({labels}) {
    const hasErrors = labels && labels.length > 0;
    if (hasErrors) {
        return (
            <div className="ErrorSidebar">
                <p className="ErrorSidebarMessage">The following fields require attention:</p>
                <ul className="ErrorSidebarList">
                    {labels.map((label) => <ErrorSidebarListItem label={label}/>)}
                </ul>
            </div>
        )
    } else {
        return <div/>
    }
}

ErrorSidebar.propTypes = {
    labels: PropTypes.arrayOf(PropTypes.string),
}
