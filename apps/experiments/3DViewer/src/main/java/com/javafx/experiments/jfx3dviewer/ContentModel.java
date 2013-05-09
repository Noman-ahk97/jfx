/*
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.javafx.experiments.jfx3dviewer;

import java.io.IOException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import com.javafx.experiments.importers.Importer3D;

/**
 * 3D Content Model for Viewer App. Contains the 3D scene and everything related to it: light, cameras etc.
 */
public class ContentModel {
    private final SubScene subScene;
    private final Group root3D = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Rotate cameraXRotate = new Rotate(-20,0,0,0,Rotate.X_AXIS);
    private final Rotate cameraYRotate = new Rotate(-20,0,0,0,Rotate.Y_AXIS);
    private final Rotate cameraLookXRotate = new Rotate(0,0,0,0,Rotate.X_AXIS);
    private final Rotate cameraLookZRotate = new Rotate(0,0,0,0,Rotate.Z_AXIS);
    private final Translate cameraPosition = new Translate(0,0,-7);
    private double dragStartX, dragStartY, dragStartRotateX, dragStartRotateY;
    private Node content;
    private AutoScalingGroup autoScalingGroup = new AutoScalingGroup(2);
    private Box xAxis,yAxis,zAxis;
    private SimpleBooleanProperty showAxis = new SimpleBooleanProperty(false){
        @Override protected void invalidated() {
            if (get()) {
                if (xAxis == null) createAxes();
                root3D.getChildren().addAll(xAxis, yAxis, zAxis);
            } else if (xAxis != null) {
                root3D.getChildren().removeAll(xAxis, yAxis, zAxis);
            }
        }
    };
    private Rotate yUpRotate = new Rotate(0,0,0,0,Rotate.X_AXIS);
    private SimpleBooleanProperty yUp = new SimpleBooleanProperty(false){
        @Override protected void invalidated() {
            if (get()) {
                yUpRotate.setAngle(180);
            } else {
                yUpRotate.setAngle(0);
            }
        }
    };

    public ContentModel() {
        subScene = new SubScene(root3D,400,400,true,false);
        subScene.setFill(Color.ALICEBLUE);

        // CAMERA
        camera.getTransforms().addAll(
                yUpRotate,
                cameraXRotate,
                cameraYRotate,
                cameraPosition,
                cameraLookXRotate,
                cameraLookZRotate);
        subScene.setCamera(camera);
        root3D.getChildren().add(camera);

        cameraPosition.zProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("z = " + newValue);
            }
        });
        //LIGHTS
//        root3D.getChildren().add(new AmbientLight(Color.WHITE));
        // BOX
//        Box testBox = new Box(5,5,5);
//        testBox.setMaterial(new PhongMaterial(Color.RED));
//        testBox.setDrawMode(DrawMode.LINE);
//        root3D.getChildren().add(testBox);

        root3D.getChildren().add(autoScalingGroup);

        // LOAD DROP HERE MODEL
        try {
            content = Importer3D.load(ContentModel.class.getResource("drop-here.obj").toExternalForm());
            autoScalingGroup.getChildren().add(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // SCENE EVENT HANDLING FOR CAMERA NAV
        subScene.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    dragStartX = event.getSceneX();
                    dragStartY = event.getSceneY();
                    dragStartRotateX = cameraXRotate.getAngle();
                    dragStartRotateY = cameraYRotate.getAngle();
                } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    double xDelta = event.getSceneX() -  dragStartX;
                    double yDelta = event.getSceneY() -  dragStartY;
                    cameraXRotate.setAngle(dragStartRotateX - (yDelta*0.7));
                    cameraYRotate.setAngle(dragStartRotateY + (xDelta*0.7));
                }
            }
        });
        subScene.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override public void handle(ScrollEvent event) {
                double z = cameraPosition.getZ()-(event.getDeltaY()*0.2);
                z = Math.max(z,-100);
                z = Math.min(z,0);
                cameraPosition.setZ(z);
            }
        });
    }

    public boolean getYUp() {
        return yUp.get();
    }

    public SimpleBooleanProperty yUpProperty() {
        return yUp;
    }

    public void setYUp(boolean yUp) {
        this.yUp.set(yUp);
    }

    public boolean getShowAxis() {
        return showAxis.get();
    }

    public SimpleBooleanProperty showAxisProperty() {
        return showAxis;
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis.set(showAxis);
    }

    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }

    public Node get3dContent() {
        return this.content;
    }

    public void set3dContent(Node content) {
        autoScalingGroup.getChildren().remove(this.content);
        this.content = content;
        autoScalingGroup.getChildren().add(this.content);
    }

    public SubScene getSubScene() {
        return subScene;
    }

    public Group getRoot3D() {
        return root3D;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public Rotate getCameraXRotate() {
        return cameraXRotate;
    }

    public Rotate getCameraYRotate() {
        return cameraYRotate;
    }

    public Translate getCameraPosition() {
        return cameraPosition;
    }

    public Rotate getCameraLookXRotate() {
        return cameraLookXRotate;
    }

    public Rotate getCameraLookZRotate() {
        return cameraLookZRotate;
    }


    private void createAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
        final Sphere red = new Sphere(50);
        red.setMaterial(redMaterial);
        final Sphere blue = new Sphere(50);
        blue.setMaterial(blueMaterial);
        xAxis = new Box(24.0, 0.05, 0.05);
        yAxis = new Box(0.05, 24.0, 0.05);
        zAxis = new Box(0.05, 0.05, 24.0);
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
    }
}
