export interface MappingValues {
    OriginTissue: string;
    TumorType: string;
    SampleDiagnosis: string;
    DataSource: string;
}

export interface Mapping {
    entityId: number;
    entityType: string;
    mappingLabels: string[];
    mappingValues: MappingValues;
    mappedTermLabel?: any;
    mapType?: any;
    justification?: any;
    status: string;
    suggestedMappings: any[];
    dateCreated?: any;
    dateUpdated?: any;
}

export interface MappingInterface {

    mappings: Mapping[];

    size: number;
    totalElements: number;
    totaPages: number;
    page: number;
    beginIndex: number;
    endIndex: number;
    currentIndex: number;
}
