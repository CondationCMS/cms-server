export interface GetTransitionsRequest {
    uri?: string;
}
export interface ItemDto {
    uri: string;
    meta?: any;
}
export interface UnpublishedPagesRequest {
    page?: number;
    size?: number;
}
export interface UnpublishedPagesDto {
    totalItems: number;
    pageSize: number;
    totalPages: number;
    page: number;
    items: ItemDto[];
}
declare const getUnpublishedPages: (options: UnpublishedPagesRequest) => Promise<UnpublishedPagesDto>;
export interface GetTransitionsDto {
    id: string;
    label: string;
}
declare const getWfTransitions: (options: GetTransitionsRequest) => Promise<{
    transitions: GetTransitionsDto[];
}>;
export interface GetWFManagerRequest {
    uri?: string;
}
export interface GetWFManagerStatusDto {
    published: boolean;
    withinSchedule: boolean;
    currentStage: string;
    publish_date?: Date;
    unpublish_date?: Date;
}
export interface GetWFManagerTransitionsDto {
    id: string;
    label: string;
    description: string;
}
export interface getWFManagerDto {
    status?: GetWFManagerStatusDto;
    transitions?: GetWFManagerTransitionsDto[];
}
declare const getWfManagerStatus: (options: GetWFManagerRequest) => Promise<getWFManagerDto>;
export interface WfTransitRequest {
    uri?: string;
    transitionId: string;
}
export interface WFTransitDto {
    success?: boolean;
}
declare const wfTransit: (options: WfTransitRequest) => Promise<WFTransitDto>;
export { getWfTransitions, getWfManagerStatus, wfTransit, getUnpublishedPages };
