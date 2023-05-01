import { Ordering } from "@shared/constants/utils/ordering";

export default interface Sorting {
    field: string;
    direction: Ordering.ASC | Ordering.DESC;
}