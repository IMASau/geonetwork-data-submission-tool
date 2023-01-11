import React from 'react';
import PropTypes from 'prop-types';
import { FeatureGroup, Map, Marker, Rectangle, TileLayer } from 'react-leaflet';
import { EditControl } from 'react-leaflet-draw';
import Control from 'react-leaflet-control';
import { Icon } from '@blueprintjs/core';


function mapToBounds({ west, east, south, north }) {
    return [[south, west], [north, east]];
}

function elementToPoint({ southBoundLatitude, westBoundLongitude }) {
    return [southBoundLatitude, westBoundLongitude];
}

function elementIsPoint({ northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude }) {
    return ((northBoundLatitude === southBoundLatitude) && (eastBoundLongitude === westBoundLongitude));
}

function elementToBounds({ northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude }) {
    return [[southBoundLatitude, westBoundLongitude], [northBoundLatitude, eastBoundLongitude]];
}

function getMaxOfArray(arr) {
    return arr.length ? Math.max.apply(null, arr) : null;
}

function getMinOfArray(arr) {
    return arr.length ? Math.min.apply(null, arr) : null;
}

function processElements(elements) {
    // ensures west bound is greater than east bound for purposes of map bounds and rectangle elements
    elements.forEach(element => element.eastBoundLongitude += element.westBoundLongitude > element.eastBoundLongitude ? 360 : 0);
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

function geometryPointToData({ coordinates }) {
    const [lng, lat] = coordinates;
    return {
        northBoundLatitude: lat,
        southBoundLatitude: lat,
        eastBoundLongitude: lng,
        westBoundLongitude: lng
    }
}

function geometryPolygonToData({ coordinates }) {
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

function ElementMarker({ element }) {
    return <Marker position={elementToPoint(element)} />
}

function ElementRectangle({ element }) {
    return <Rectangle bounds={elementToBounds(element)} />
}

function renderElement(element, idx) {
    if (elementIsPoint(element)) {
        return <ElementMarker element={element} key={idx} />
    } else {
        return <ElementRectangle element={element} key={idx} />
    }
}

const BaseLayer = () => (
    <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution="&copy; <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"
    />
)

const SatelliteBaseLayer = () => (
    <TileLayer
        url="https://mt1.google.com/vt/lyrs=s&x={x}&y={y}&z={z}"
        attribution="Map data ©2015 Google"
    />
)

// TODO: disabled mode?
export const BoxMap = ({ mapWidth, elements, onChange, tickId }) => {

    const featureGroupRef = React.useRef(null);

    const defaultCenter = [-28, 134];

    processElements(elements);
    const bounds = elementsToExtents(elements);
    const setCenter = bounds && bounds.north == bounds.south && bounds.east == bounds.west;
    const setBounds = bounds && !setCenter;

    const handleChange = () => {
        const geoJson = featureGroupRef.current.leafletElement.toGeoJSON()

        // wrap longitude values to -180 +180 range
        geoJson.features.forEach(feature => {
            const coordinates = feature.geometry.coordinates[0];
            for (let i = 0; i < coordinates.length; i++)
                coordinates[i][0] = Math.round(((coordinates[i][0] % 360 + 540) % 360 - 180) * 1000000) / 1000000;
        });

        onChange(geoJson);
    };

    const [useSatellite, setUseSatellite] = React.useState(false);

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
            {useSatellite ? <SatelliteBaseLayer /> : <BaseLayer />}
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
            <Control
                position="topleft"
                className="leaflet-bar"
            >
                <a
                    onClick={e => {
                        e.preventDefault();
                        setUseSatellite(!useSatellite);
                    }}
                >
                    <Icon icon={useSatellite ? "satellite" : "map"} size={14} />
                </a>
            </Control>
        </Map>
    );
};

BoxMap.propTypes = {
    mapWidth: PropTypes.number,
    elements: PropTypes.arrayOf(PropTypes.shape({
        northBoundLatitude: PropTypes.number.isRequired,
        westBoundLongitude: PropTypes.number.isRequired,
        southBoundLatitude: PropTypes.number.isRequired,
        eastBoundLongitude: PropTypes.number.isRequired,
    })),
    onChange: PropTypes.func.isRequired,
    tickId: PropTypes.string,
};
