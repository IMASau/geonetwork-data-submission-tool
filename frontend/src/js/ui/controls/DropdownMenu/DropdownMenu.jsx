import React from 'react';
import PropTypes from 'prop-types';
import * as BPCore from "@blueprintjs/core";
import {hasErrorIntent, useCachedState} from "../utils";

function MenuItem(menuItem, index) {
	const {divider, text, icon, onClick} = menuItem
	
	return divider  
	? <BPCore.Menu.Divider key={index}/>
	: <BPCore.Menu.Item key={index} text={text} icon={icon} onClick={onClick}/>;
}

export function DropdownMenu({text, icon, menuItems}) {
	return (
		<BPCore.Popover
			content={<BPCore.Menu>
				{menuItems.map((menuItem, index) => MenuItem(menuItem, index))}
			</BPCore.Menu>}
		>
			<BPCore.Button icon={icon} text={text}/>
		</BPCore.Popover>
	);
}

DropdownMenu.propTypes = {
    text: PropTypes.string.isRequired,
    icon: PropTypes.string,
    menuItems: PropTypes.arrayOf(PropTypes.object).isRequired
}