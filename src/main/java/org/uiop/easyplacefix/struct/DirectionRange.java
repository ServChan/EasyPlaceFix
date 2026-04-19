package org.uiop.easyplacefix.struct;

import net.minecraft.core.Direction;

public enum DirectionRange {
    North_Range(136f,-136f,Direction.NORTH ),
    South_Range(-44f,44f,Direction.SOUTH ),
    West_Range(46f,134f, Direction.WEST),
    East_Range(-46f,-134f,Direction.EAST ),
    Up_Range(-46f,-90f, Direction.UP),
    Horizontal_range(44f,-44f,null),
    Down_Range(46f,90f,Direction.DOWN );
    // Float bounds
    private final float firstValue;
    private final float secondValue;
    private final Direction direction;

    // Enum constructor
    DirectionRange(float firstValue, float secondValue, Direction direction) {
        this.firstValue = firstValue;
        this.secondValue=secondValue;
        this.direction = direction;
    }

    // Get first float bound
    public float getFirstValue() {
        return firstValue;
    }
    public float getSecondValue() {
        return secondValue;
    }
    public Direction getDirection(){
        return direction;
    }
    public  static DirectionRange  DirectionToRange(Direction direction){
       if (direction==null)
           return DirectionRange.Horizontal_range;
        switch (direction){
            case NORTH -> {
                return North_Range;
            }
            case SOUTH -> {
                return South_Range;
            }
            case WEST -> {
                return West_Range;
            }
            case EAST -> {
                return East_Range;
            }
            case UP -> {
                return Up_Range;
            }
            case DOWN -> {
                return Down_Range;
            }

        }
        return Horizontal_range;
    }
    public  boolean isInRange(Direction direction){
        return direction == this.direction;
    }

}
