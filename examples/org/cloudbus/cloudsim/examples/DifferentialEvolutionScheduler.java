package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DifferentialEvolutionScheduler extends DatacenterBroker {

    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;

    public DifferentialEvolutionScheduler(String name) throws Exception {
        super(name);
        this.cloudletList = new ArrayList<>();
        this.vmList = new ArrayList<>();
    }

    // Differential Evolution scheduling method
    protected void runDifferentialEvolutionScheduling() {
        // Differential Evolution parameters
        int populationSize = 10;
        int maxGenerations = 50;
        double mutationFactor = 0.8;
        double crossoverRate = 0.9;

        // Initialize DE population
        List<int[]> population = initializePopulation(populationSize, cloudletList.size(), vmList.size());

        // Evolve population
        for (int generation = 0; generation < maxGenerations; generation++) {
            List<int[]> newPopulation = new ArrayList<>();
            for (int[] individual : population) {
                int[] trial = mutateAndCrossover(individual, population, mutationFactor, crossoverRate);
                if (evaluateFitness(trial) < evaluateFitness(individual)) {
                    newPopulation.add(trial);
                } else {
                    newPopulation.add(individual);
                }
            }
            population = newPopulation;
        }

        // Find the best solution
        int[] bestSolution = getBestSolution(population);

        // Apply the optimized Cloudlet-to-VM mapping
        allocateCloudletsToVms(bestSolution);
    }

    @Override
    public void submitCloudletList(List<? extends Cloudlet> list) {
        super.submitCloudletList(list);
        this.cloudletList = (List<Cloudlet>) list;
    }

    @Override
    public void submitVmList(List<? extends Vm> list) {
        super.submitVmList(list);
        this.vmList = (List<Vm>) list;
    }

    private List<int[]> initializePopulation(int size, int cloudletCount, int vmCount) {
        Random random = new Random();
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int[] individual = new int[cloudletCount];
            for (int j = 0; j < cloudletCount; j++) {
                individual[j] = random.nextInt(vmCount); // Randomly assign Cloudlets to VMs
            }
            population.add(individual);
        }
        return population;
    }

    private int[] mutateAndCrossover(int[] individual, List<int[]> population, double mutationFactor, double crossoverRate) {
        Random random = new Random();
        int populationSize = population.size();

        // Select three distinct individuals randomly
        int r1, r2, r3;
        do {
            r1 = random.nextInt(populationSize);
            r2 = random.nextInt(populationSize);
            r3 = random.nextInt(populationSize);
        } while (r1 == r2 || r1 == r3 || r2 == r3);

        int[] donor = new int[individual.length];
        for (int i = 0; i < individual.length; i++) {
            donor[i] = (int) Math.round(population.get(r1)[i] + mutationFactor * (population.get(r2)[i] - population.get(r3)[i]));
            donor[i] = Math.max(0, Math.min(donor[i], vmList.size() - 1)); // Keep values within bounds
        }

        // Crossover to create a trial vector
        int[] trial = new int[individual.length];
        for (int i = 0; i < individual.length; i++) {
            if (random.nextDouble() < crossoverRate) {
                trial[i] = donor[i];
            } else {
                trial[i] = individual[i];
            }
        }

        return trial;
    }

    private double evaluateFitness(int[] individual) {
        double fitness = 0.0;

        // Simulate the total waiting time based on Cloudlet-to-VM mapping
        for (int i = 0; i < individual.length; i++) {
            Vm assignedVm = vmList.get(individual[i]);
            Cloudlet cloudlet = cloudletList.get(i);

            // Example fitness calculation: assume each Cloudlet adds a fixed penalty
            fitness += cloudlet.getCloudletLength() / assignedVm.getMips();
        }

        return fitness;
    }

    private int[] getBestSolution(List<int[]> population) {
        int[] best = null;
        double bestFitness = Double.MAX_VALUE;

        for (int[] individual : population) {
            double fitness = evaluateFitness(individual);
            if (fitness < bestFitness) {
                best = individual;
                bestFitness = fitness;
            }
        }

        return best;
    }

    private void allocateCloudletsToVms(int[] mapping) {
        for (int i = 0; i < mapping.length; i++) {
            int vmIndex = mapping[i];
            Vm vm = vmList.get(vmIndex);
            Cloudlet cloudlet = cloudletList.get(i);
            cloudlet.setVmId(vm.getId());
        }
    }
}
