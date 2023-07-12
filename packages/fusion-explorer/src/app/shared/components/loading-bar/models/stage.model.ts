/**
 * Handles loading bar stages
 */
export interface Stage {
    /** fill state [01] */
    fill: number
    /** number of milliseconds before passing to next state automatically */
    autoNext?: number
    /** number of milliseconds to step animation from n-1 to n stage */
    fillDuration?: number
}