package org.pdxfinder.commands;

public abstract class LoaderBase {

    /* The Template method */
    public final void play() {

        step00GetMetaDataFolder();

        step01GetMetaDataFile();

        step02CreateProviderGroup();

        step03CreateNSGammaHostStrain();

        step04CreateNSHostStrain();

        step05CreateProjectGroup();

        step06GetPDXModels();

        step07GetMetaData();

        step08LoadPatientData();

        step09CreateModels();

        step10CreateEngraftmentsAndSpecimen();

        createCurrentTreatment();
    }

    abstract void step00GetMetaDataFolder();

    abstract void step01GetMetaDataFile();

    abstract void step02CreateProviderGroup();

    abstract void step03CreateNSGammaHostStrain();

    abstract void step04CreateNSHostStrain();

    abstract void step05CreateProjectGroup();

    abstract void step06GetPDXModels();

    abstract void step07GetMetaData();

    abstract void step08LoadPatientData();

    abstract void step09CreateModels();

    abstract void step10CreateEngraftmentsAndSpecimen();

    abstract void createCurrentTreatment();


}
