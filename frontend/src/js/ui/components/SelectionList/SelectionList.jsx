import React from 'react';
import PropTypes from 'prop-types';
import {DragDropContext, Draggable, Droppable} from "react-beautiful-dnd";
import {useCachedState} from "../utils";

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

// NOTE: Attempts to workaround glitch on recorder by caching state
// NOTE: Component should change key to flush invalid state if necessary
export function SelectionList({items, onReorder, disabled, renderItem}) {

    const [stateValue, setStateValue] = useCachedState(items);

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
                            <Draggable key={item.value}
                                       draggableId={item.value}
                                       index={index}
                                       isDragDisabled={disabled}>
                                {(provided, snapshot) => (
                                    <div
                                        ref={provided.innerRef}
                                        {...provided.draggableProps}
                                        {...provided.dragHandleProps}
                                        style={getItemStyle(
                                            snapshot.isDragging,
                                            provided.draggableProps.style
                                        )}
                                    >
                                        {renderItem({item})}
                                    </div>
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
    })),
    onReorder: PropTypes.func,
    renderItem: PropTypes.func,
    disabled: PropTypes.bool,
}


export function SimpleListItem({item}) {
    return <div>{item.label}</div>
}

SimpleListItem.propTypes = {
    item: PropTypes.shape({
        value: PropTypes.string,
        label: PropTypes.string
    }),
}
