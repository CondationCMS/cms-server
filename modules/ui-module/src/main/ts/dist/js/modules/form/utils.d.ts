declare const createID: () => string;
declare const utcToLocalDateTimeInputValue: (utcString: string) => string;
declare function getUTCDateTimeFromInput(inputElement: HTMLInputElement): string | null;
declare function utcToLocalDateInputValue(utcString: string): string;
declare function getUTCDateFromInput(inputElement: HTMLInputElement): string | null;
export { createID, utcToLocalDateTimeInputValue, getUTCDateTimeFromInput, utcToLocalDateInputValue, getUTCDateFromInput };
