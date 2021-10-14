import React from 'react';
import PropTypes from 'prop-types';
import {FeatureGroup, Map, Marker, Rectangle, TileLayer} from 'react-leaflet';
import {EditControl} from 'react-leaflet-draw';

function mapToBounds({west, east, south, north}) {
    return [[south, west], [north, east]];
}

function elementToPoint({southBoundLatitude, westBoundLongitude}) {
    return [southBoundLatitude, westBoundLongitude];
}

function elementIsPoint({northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude}) {
    return ((northBoundLatitude === southBoundLatitude) && (eastBoundLongitude === westBoundLongitude));
}

function elementToBounds({northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude}) {
    return [[southBoundLatitude, westBoundLongitude], [northBoundLatitude, eastBoundLongitude]];
}

function getMaxOfArray(arr) {
    return arr.length ? Math.max.apply(null, arr) : null;
}

function getMinOfArray(arr) {
    return arr.length ? Math.min.apply(null, arr) : null;
}

function elementsToExtents(elements) {
    var north = getMaxOfArray(elements.map((ele) => ele.northBoundLatitude));
    var west = getMinOfArray(elements.map((ele) => ele.westBoundLongitude));
    var south = getMinOfArray(elements.map((ele) => ele.southBoundLatitude));
    var east = getMaxOfArray(elements.map((ele) => ele.eastBoundLongitude));
    if (north && west && south && east) {
        return {
            "north": north,
            "west": west,
            "east": east,
            "south": south
        }
    }
}

function geometryPointToData({coordinates}) {
    const [lng, lat] = coordinates;
    return {
        northBoundLatitude: lat,
        southBoundLatitude: lat,
        eastBoundLongitude: lng,
        westBoundLongitude: lng
    }
}

function geometryPolygonToData({coordinates}) {
    const [points] = coordinates;
    const lngs = points.map(([a, _]) => a);
    const lats = points.map(([_, b]) => b);
    return {
        northBoundLatitude: getMaxOfArray(lats),
        southBoundLatitude: getMinOfArray(lats),
        eastBoundLongitude: getMaxOfArray(lngs),
        westBoundLongitude: getMinOfArray(lngs)
    }
}

function ElementMarker({element}) {
    return <Marker position={elementToPoint(element)}/>
}

function ElementRectangle({element}) {
    return <Rectangle bounds={elementToBounds(element)}/>
}

function renderElement(element, idx) {
    if (elementIsPoint(element)) {
        return <ElementMarker element={element} key={idx}/>
    } else {
        return <ElementRectangle element={element} key={idx}/>
    }
}

const BaseLayer = () => (
    <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution="&copy; <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"
    />
)

// TODO: disabled mode?
export const BoxMap = ({mapWidth, elements, onChange, tickId}) => {

    const featureGroupRef = React.useRef(null);

    const defaultCenter = [-28, 134];

    const bounds = elementsToExtents(elements);
    const setCenter = bounds && bounds.north == bounds.south && bounds.east == bounds.west;
    const setBounds = bounds && !setCenter;
    console.log({bounds, setCenter, setBounds})

    const handleChange = () => {
        onChange(featureGroupRef.current.leafletElement.toGeoJSON())
    };

    return (
        <Map
            id="map"
            style={{
                "height": 500,
                "width": mapWidth,
                "zIndex": 10,
            }}
            useFlyTo={true}
            center={setCenter ? [bounds.south, bounds.west] : defaultCenter}
            zoom={4}
            keyboard={false}
            closePopupOnClick={false}
            bounds={setBounds ? [[bounds.south, bounds.west], [bounds.north, bounds.east]] : undefined}
        >
            <BaseLayer/>
            <FeatureGroup
                ref={featureGroupRef}
                key={"featureGroup" + tickId}
            >
                <EditControl
                    position="topright"
                    draw={{
                        "polyline": false,
                        "polygon": false,
                        "rectangle": {},
                        "circle": false,
                        "marker": {},
                        "circlemarker": false
                    }}
                    edit={{
                        edit: {},
                        remove: {},
                        poly: {}
                    }}
                    onEdited={handleChange}
                    onDeleted={handleChange}
                    onCreated={handleChange}
                />
                {elements.map(renderElement)}
            </FeatureGroup>
        </Map>
    );
};

BoxMap.propTypes = {
    boxes: PropTypes.array
};
