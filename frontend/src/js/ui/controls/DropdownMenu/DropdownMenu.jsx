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

export function DropdownMenu({disabled, text, icon, placement, className, menuItems, onClick}) {
	return onClick
	? <BPCore.Button
		disabled={disabled}
		icon={icon}
		text={text}
		className={className}
		onClick={onClick}
	/>
	: (
		<BPCore.Popover
			disabled={disabled}
			content={<BPCore.Menu>
				{menuItems.map((menuItem, index) => MenuItem(menuItem, index))}
			</BPCore.Menu>}
			placement={placement}
		>
			<BPCore.Button
				disabled={disabled}
				icon={icon}
				text={text}
				className={className}
			/>
		</BPCore.Popover>
	);
}

DropdownMenu.propTypes = {
	disabled: PropTypes.bool,
    text: PropTypes.string.isRequired,
    icon: PropTypes.string,
	placement: PropTypes.string,
	className: PropTypes.string,
    menuItems: PropTypes.arrayOf(PropTypes.object).isRequired,
	onClick: PropTypes.func
}