import React from 'react';
import PropTypes from 'prop-types';
import { DragDropContext, Draggable, Droppable } from "react-beautiful-dnd";
import { useCachedState } from "../utils";
import * as ReactDOM from "react-dom";
import { BreadcrumbListItem, SimpleListItem, TableListItem } from "../ListItem/ListItem";
import * as BPCore from "@blueprintjs/core"

const getItemStyle = (isDragging, draggableStyle) => ({
    // some basic styles to make the items look a bit nicer
    userSelect: "none",

    // change background colour if dragging
    background: isDragging ? "rgba(.6,0.6,0.6,.25)" : null,

    // styles we need to apply on draggables
    ...draggableStyle
});

const getListStyle = isDraggingOver => ({
    background: isDraggingOver ? "lightblue" : null,
});


function reorder(list, startIndex, endIndex) {
    const result = Array.from(list);
    const [removed] = result.splice(startIndex, 1);
    result.splice(endIndex, 0, removed);
    return result;
}

function DragHandle() {
    return <BPCore.Icon icon="drag-handle-vertical" />
}

function RemoveButton({ index, onRemoveClick, disabled }) {
    if (!onRemoveClick) {
        return null
    } else {
        return (
            <BPCore.Button icon="cross"
                small={true}
                minimal={true}
                disabled={disabled}
                onClick={_ => onRemoveClick(index)} />
        )
    }
}

function ItemLabel({ itemProps, item, index, onItemClick, renderItem }) {
    const body = renderItem({ ...itemProps, item, index });
    const onClick = onItemClick ? (_ => onItemClick(index)) : null;
    return (
        <div onClick={onClick}>
            {body}
        </div>
    )
}

// NOTE: Workaround for conflict with blueprint expander (transform related?)
// https://github.com/vtereshyn/react-beautiful-dnd-ru/blob/master/docs/patterns/using-a-portal.md
const portal = document.createElement('div');
portal.classList.add('SelectionListPortal');

if (!document.body) {
    throw new Error('body not ready for portal creation!');
} else {
    document.body.appendChild(portal);
}

function PortalAwareItem({ provided, snapshot, itemLabel, removeButton }) {

    const usePortal = snapshot.isDragging;

    const child = (
        <div
            ref={provided.innerRef}
            {...provided.draggableProps}
            style={getItemStyle(
                snapshot.isDragging,
                provided.draggableProps.style
            )}
            className="DragHandleWrapper"
        >
            <div className="DragHandleWrapperHandle"
                {...provided.dragHandleProps}>
                <DragHandle />
            </div>
            <div className="DragHandleWrapperLabel">
                {itemLabel}
            </div>
            <div className="DragHandleWrapperRemoveButton">
                {removeButton}
            </div>
        </div>
    );

    if (!usePortal) {
        return child;
    } else {
        return ReactDOM.createPortal(child, portal);
    }
}

// NOTE: Attempts to workaround glitch on recorder by caching state
// NOTE: Component should change key to flush invalid state if necessary
export function SelectionList({ items, itemProps, getValue, onReorder, onItemClick, onRemoveClick, disabled, renderItem }) {

    const [stateValue, setStateValue] = useCachedState(items);
    const isDragDisabled = disabled || !onReorder;

    const onDragEnd = (result) => {
        if (result.destination) {
            // Optimistially reorder to avoid glitch
            setStateValue(reorder(
                stateValue,
                result.source.index,
                result.destination.index
            ))
            onReorder(
                result.source.index,
                result.destination.index
            )
        }
    }

    return (
        <DragDropContext onDragEnd={onDragEnd}>
            <Droppable droppableId="droppable">
                {(provided, snapshot) => (
                    <div
                        {...provided.droppableProps}
                        ref={provided.innerRef}
                        style={getListStyle(snapshot.isDraggingOver)}
                    >
                        {stateValue.map((item, index) => (
                            <Draggable key={getValue(item)}
                                draggableId={getValue(item)}
                                index={index}
                                isDragDisabled={isDragDisabled}>
                                {(provided, snapshot) => (
                                    <PortalAwareItem
                                        provided={provided}
                                        snapshot={snapshot}
                                        itemLabel={ItemLabel({ itemProps, item, index, onItemClick, renderItem })}
                                        removeButton={RemoveButton({ disabled, onRemoveClick, index })}
                                    />
                                )}
                            </Draggable>
                        ))}
                        {provided.placeholder}
                    </div>
                )}
            </Droppable>
        </DragDropContext>
    )
}

SelectionList.propTypes = {
    items: PropTypes.arrayOf(PropTypes.shape({
        value: PropTypes.string,
        label: PropTypes.string
    })).isRequired,
    onReorder: PropTypes.func,
    onItemClick: PropTypes.func,
    onRemoveClick: PropTypes.func,
    renderItem: PropTypes.func.isRequired,
    getValue: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
}

export function TableSelectionList({ items, getValue, onReorder, onRemoveClick, disabled, columns }) {
    return (
        <SelectionList
            items={items}
            onReorder={onReorder}
            onRemoveClick={onRemoveClick}
            getValue={getValue}
            disabled={disabled}
            renderItem={({ item }) => <TableListItem item={item} columns={columns} onRemoveClick={onRemoveClick} disabled={disabled} />}>
        </SelectionList>
    )
}

TableSelectionList.propTypes = {
    items: PropTypes.arrayOf(PropTypes.object).isRequired,
    getValue: PropTypes.func.isRequired,
    onReorder: PropTypes.func,
    onItemClick: PropTypes.func,
    onRemoveClick: PropTypes.func,
    disabled: PropTypes.bool,
    columns: PropTypes.arrayOf(PropTypes.shape({
        flex: PropTypes.number.isRequired,
        getLabel: PropTypes.func.isRequired,
    }))
}

export function BreadcrumbSelectionList({ items, getValue, getLabel, getBreadcrumb, onReorder, onRemoveClick, disabled }) {
    return (
        <SelectionList
            items={items}
            onReorder={onReorder}
            onRemoveClick={onRemoveClick}
            getValue={getValue}
            disabled={disabled}
            renderItem={({ item, index }) => (
                <BreadcrumbListItem
                    item={item}
                    index={index}
                    onRemoveClick={onRemoveClick}
                    disabled={disabled}
                    getBreadcrumb={getBreadcrumb}
                    getLabel={getLabel} />
            )}>
        </SelectionList>
    )
}

BreadcrumbSelectionList.propTypes = {
    items: PropTypes.arrayOf(PropTypes.object).isRequired,
    getValue: PropTypes.func.isRequired,
    getLabel: PropTypes.func.isRequired,
    getBreadcrumb: PropTypes.func.isRequired,
    onReorder: PropTypes.func,
    onItemClick: PropTypes.func,
    onRemoveClick: PropTypes.func,
    disabled: PropTypes.bool,
}

export function SimpleSelectionList({ items, getValue, getLabel, onReorder, onRemoveClick, disabled }) {
    return (
        <SelectionList
            items={items}
            onReorder={onReorder}
            onRemoveClick={onRemoveClick}
            getValue={getValue}
            disabled={disabled}
            renderItem={({ item, index }) => (
                <SimpleListItem
                    item={item}
                    index={index}
                    onRemoveClick={onRemoveClick}
                    disabled={disabled}
                    getLabel={getLabel} />
            )}>
        </SelectionList>
    )
}

SimpleSelectionList.propTypes = {
    items: PropTypes.arrayOf(PropTypes.object).isRequired,
    getValue: PropTypes.func.isRequired,
    getLabel: PropTypes.func.isRequired,
    onReorder: PropTypes.func,
    onItemClick: PropTypes.func,
    onRemoveClick: PropTypes.func,
    disabled: PropTypes.bool,
}
