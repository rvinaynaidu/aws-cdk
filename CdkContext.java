package com.capgroup.digital.pss.fcr.services.infra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CdkContext {
	
	
	private Environment environment;
	
	private String clusterName;
	
	private String clusterArn;
	
	private ServiceDefinition service;
	
	private Map<String, String> tags;
	
	private String vpcId;
	
	private List<SubnetGroup> subnetGroups;
	
	private List<String> securityGroups;
	
	
	
	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	

	public String getClusterArn() {
		return clusterArn;
	}

	public void setClusterArn(String clusterArn) {
		this.clusterArn = clusterArn;
	}

	public ServiceDefinition getService() {
		return service;
	}

	public void setService(ServiceDefinition service) {
		this.service = service;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public String getVpcId() {
		return vpcId;
	}

	public void setVpcId(String vpcId) {
		this.vpcId = vpcId;
	}

	public List<SubnetGroup> getSubnetGroups() {
		return subnetGroups;
	}

	public void setSubnetGroups(List<SubnetGroup> subnetGroups) {
		this.subnetGroups = subnetGroups;
	}

	public List<String> getSecurityGroups() {
		return securityGroups;
	}

	public void setSecurityGroups(List<String> securityGroups) {
		this.securityGroups = securityGroups;
	}

	protected static class Environment {
		
		private String account;
		
		private String region;
		
		private String name;

		public String getAccount() {
			return account;
		}

		public void setAccount(String account) {
			this.account = account;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
		
		
		
		
	}
	
	protected static class SubnetGroup {
		
		private String name;
		
		private String subnetId;
		
		private String az;
		
		private String routeTableId;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSubnetId() {
			return subnetId;
		}

		public void setSubnetId(String subnetId) {
			this.subnetId = subnetId;
		}

		public String getAz() {
			return az;
		}

		public void setAz(String az) {
			this.az = az;
		}

		public String getRouteTableId() {
			return routeTableId;
		}

		public void setRouteTableId(String routeTableId) {
			this.routeTableId = routeTableId;
		}
		
		
	}
	
	protected static class ServiceDefinition {
		
		private String name;
		
		private int memory;
		
		private int cpu;
		
		private int count;
		
		private String serviceContainerImage;
		
		private String targetGroupHealth;
		
		
		
		private Map<String, String> envVariables = new HashMap<String, String>();
		
		

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getMemory() {
			return memory;
		}

		public void setMemory(int memory) {
			this.memory = memory;
		}

		public int getCpu() {
			return cpu;
		}

		public void setCpu(int cpu) {
			this.cpu = cpu;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public Map<String, String> getEnvVariables() {
			return envVariables;
		}

		public void setEnvVariables(Map<String, String> envVariables) {
			this.envVariables = envVariables;
		}

		public String getServiceContainerImage() {
			return serviceContainerImage;
		}

		public void setServiceContainerImage(String serviceContainerImage) {
			this.serviceContainerImage = serviceContainerImage;
		}

		public String getTargetGroupHealth() {
			return targetGroupHealth;
		}

		public void setTargetGroupHealth(String targetGroupHealth) {
			this.targetGroupHealth = targetGroupHealth;
		}
		
		
		
		
	}

	@Override
	public String toString() {
		return "CdkContext [environment=" + environment + ", clusterName=" + clusterName + ", service=" + service
				+ ", tags=" + tags + ", vpcId=" + vpcId + ", subnetGroups=" + subnetGroups + ", securityGroups="
				+ securityGroups + "]";
	}
	
	
	

}
