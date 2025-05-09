package com.filevault.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * Utility class for creating and applying animations in the application.
 */
public class AnimationUtil {
    
    /**
     * Creates a fade-in animation for a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @return The configured transition
     */
    public static FadeTransition createFadeInTransition(Node node, double duration) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(duration), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);
        return fadeIn;
    }
    
    /**
     * Creates a fade-out animation for a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @return The configured transition
     */
    public static FadeTransition createFadeOutTransition(Node node, double duration) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(duration), node);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);
        return fadeOut;
    }
    
    /**
     * Creates a scale-in animation for a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @return The configured transition
     */
    public static ScaleTransition createScaleInTransition(Node node, double duration) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(duration), node);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        return scaleIn;
    }
    
    /**
     * Creates a slide-up animation for a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @return The configured transition
     */
    public static TranslateTransition createSlideUpTransition(Node node, double duration) {
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(duration), node);
        slideUp.setFromY(20);
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);
        return slideUp;
    }
    
    /**
     * Creates a rotation animation for a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @param cycles Number of cycles (-1 for infinite)
     * @return The configured transition
     */
    public static RotateTransition createRotateTransition(Node node, double duration, int cycles) {
        RotateTransition rotate = new RotateTransition(Duration.millis(duration), node);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(cycles);
        rotate.setInterpolator(Interpolator.LINEAR);
        return rotate;
    }
    
    /**
     * Applies a combined fade-in and scale-in animation to a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     */
    public static void applyFadeInWithScale(Node node, double duration) {
        node.setOpacity(0);
        
        FadeTransition fadeIn = createFadeInTransition(node, duration);
        ScaleTransition scaleIn = createScaleInTransition(node, duration);
        
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
        parallelTransition.play();
    }
    
    /**
     * Applies a combined fade-in and slide-up animation to a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     */
    public static void applyFadeInWithSlideUp(Node node, double duration) {
        node.setOpacity(0);
        
        FadeTransition fadeIn = createFadeInTransition(node, duration);
        TranslateTransition slideUp = createSlideUpTransition(node, duration);
        
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideUp);
        parallelTransition.play();
    }
    
    /**
     * Creates a pulse animation for a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @return The configured transition
     */
    public static ScaleTransition createPulseTransition(Node node, double duration) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(duration), node);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        return pulse;
    }
    
    /**
     * Creates a transition between two views, fading one out and another in.
     * 
     * @param currentView The current view to fade out
     * @param newView The new view to fade in
     * @param duration Duration in milliseconds
     * @return The configured sequential transition
     */
    public static SequentialTransition createViewTransition(Node currentView, Node newView, double duration) {
        newView.setOpacity(0);
        
        FadeTransition fadeOutCurrent = createFadeOutTransition(currentView, duration / 2);
        PauseTransition pause = new PauseTransition(Duration.millis(duration / 10));
        FadeTransition fadeInNew = createFadeInTransition(newView, duration / 2);
        
        SequentialTransition sequentialTransition = new SequentialTransition(fadeOutCurrent, pause, fadeInNew);
        return sequentialTransition;
    }
    
    /**
     * Creates a glow effect animation for a node.
     * 
     * @param node The node to animate
     * @param duration Duration in milliseconds
     * @param cycles Number of cycles
     */
    public static void applyGlowPulse(Node node, double duration, int cycles) {
        if (!(node instanceof Region)) {
            return;
        }
        
        Region region = (Region) node;
        String originalStyle = region.getStyle();
        String glowStyle = originalStyle + "-fx-effect: dropshadow(gaussian, #6A4BAF, 20, 0.8, 0, 0);";
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(region.styleProperty(), originalStyle)
            ),
            new KeyFrame(Duration.millis(duration / 2),
                new KeyValue(region.styleProperty(), glowStyle)
            ),
            new KeyFrame(Duration.millis(duration),
                new KeyValue(region.styleProperty(), originalStyle)
            )
        );
        
        timeline.setCycleCount(cycles);
        timeline.play();
    }
    
    /**
     * Creates a loading spinner animation.
     * 
     * @param node The node to animate as a spinner
     * @return The configured rotation transition
     */
    public static RotateTransition createLoadingSpinner(Node node) {
        RotateTransition rotate = createRotateTransition(node, 1500, -1);
        rotate.play();
        return rotate;
    }
    
    /**
     * Stops a loading spinner animation.
     * 
     * @param rotateTransition The rotation transition to stop
     */
    public static void stopLoadingSpinner(RotateTransition rotateTransition) {
        if (rotateTransition != null) {
            rotateTransition.stop();
        }
    }
} 