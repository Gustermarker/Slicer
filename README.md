# Slicer
A 3D printer slicer software built from scratch. Currently a work in progress. 

What is a slicer? A slicer takes in a 3D model (STL file), "slices" it into a bunch of horizontal layers, and generates a toolpath (in GCODE) from these newly created 2D layers. The toolpath is the path that the 3d printer takes to lay down the plastic. Note: 3D printing is a bit of a misnomer. It would more accurately be described as 2.5D printing, as at no point are all 3 axes moving. It lays down plastic in XY, and Z advances to the next layer when done. 

To do list:
1) Parse STL file into some usable data
2) Slice the model, creates n amount of 2D layers
