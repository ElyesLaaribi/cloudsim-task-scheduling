package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
//import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class CloudSimExample1 {

    public static void main(String[] args) {
        try {
            // Step 1: Initialize the CloudSim library
            int numUsers = 1; // Number of users
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false; // No event tracing
            CloudSim.init(numUsers, calendar, traceFlag);

            // Step 2: Create Datacenter
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");

            // Step 3: Create Broker
            DatacenterBroker broker = new DatacenterBroker("DefaultBroker");

            // Step 4: Create VMs and Cloudlets
            List<Vm> vmList = createVMs(broker.getId()); // Associate VMs with Broker ID
            List<Cloudlet> cloudletList = createCloudlets(broker.getId()); // Associate Cloudlets with Broker ID

            // Step 5: Submit VM and Cloudlet lists to the broker
            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // Step 6: Start the simulation
            CloudSim.startSimulation();

            // Step 7: Collect results after simulation ends
            List<Cloudlet> finalCloudletResults = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            // Step 8: Print results
            printCloudletResults(finalCloudletResults);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to create VMs
    private static List<Vm> createVMs(int brokerId) {
        List<Vm> vmList = new ArrayList<>();
        int mips = 1000; // MIPS for each VM
        int ram = 2048; // VM memory (MB)
        long storage = 10000; // VM storage
        int bw = 1000; // Bandwidth
        int pesNumber = 1; // Number of PEs
        String vmm = "Xen"; // Virtual machine monitor

        // Create 4 VMs
        for (int i = 0; i < 4; i++) {
            vmList.add(new Vm(i, brokerId, mips, pesNumber, ram, bw, storage, vmm, 
                    new CloudletSchedulerTimeShared())); // Cloudlet Scheduler
        }

        return vmList;
    }

    // Method to create Datacenter
    private static Datacenter createDatacenter(String name) {
        // Create Hosts
        List<Host> hostList = new ArrayList<>();

        int mips = 1000; // MIPS for each Host
        int ram = 4096; // Host memory (MB)
        long storage = 100000; // Host storage
        int bw = 10000; // Host bandwidth

        // Create a list of PEs for each Host
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // Add a single PE

        // Create Hosts and add them to the Host list
        for (int i = 0; i < 2; i++) { // Two Hosts in the datacenter
            hostList.add(new Host(
                    i,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList) // TimeShared Scheduler
            ));
        }

        // Create Datacenter Characteristics
        String arch = "x86"; // System architecture
        String os = "Linux"; // Operating system
        String vmm = "Xen"; // Virtual machine monitor
        double timeZone = 10.0; // Time zone
        double cost = 3.0; // Cost per second
        double costPerMem = 0.05; // Cost per MB of memory
        double costPerStorage = 0.1; // Cost per MB of storage
        double costPerBw = 0.1; // Cost per MB of bandwidth

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone, cost, costPerMem, costPerStorage, costPerBw);

        // Create the Datacenter
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList),
                    new LinkedList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    // Method to create Cloudlets
    private static List<Cloudlet> createCloudlets(int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        int pesNumber = 1; // Number of PEs required by each Cloudlet
        long length = 10000; // Cloudlet length
        long fileSize = 300; // Cloudlet file size
        long outputSize = 300; // Cloudlet output size
        UtilizationModel utilizationModel = new UtilizationModelFull(); // Full utilization

        // Create 6 Cloudlets
        for (int i = 0; i < 6; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, 
                    utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId); // Associate Cloudlet with Broker ID
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }

    // Method to print Cloudlet results
    private static void printCloudletResults(List<Cloudlet> cloudletList) {
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID    STATUS    Datacenter ID    VM ID    Execution Time");

        for (Cloudlet cloudlet : cloudletList) {
            System.out.printf("%-15d%-10s%-15d%-10d%-15.2f\n",
                    cloudlet.getCloudletId(),
                    cloudlet.getStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "FAILED",
                    cloudlet.getResourceId(),
                    cloudlet.getVmId(),
                    cloudlet.getActualCPUTime());
            
            //System.out.printf("Example1 Differential Evolution finished!");
        }
        
    }
}
