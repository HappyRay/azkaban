#! /usr/bin/env python3
import subprocess


def update_tags():
    """
    Update the tags in the local branch from the remote.
    """
    run_cmd('git checkout master')
    run_cmd('git pull')
    pass


def run_cmd(cmd):
    print("Running cmd: " + cmd)
    subprocess.run(cmd, capture_output=False, check=True, text=True, shell=True)


def get_latest_tag():
    cmd = 'git describe --abbrev=0'
    output = subprocess.run(cmd, capture_output=True, check=True, text=True, shell=True)
    latest_tag = output.stdout
    print("Latest tag : {}".format(latest_tag))
    return latest_tag


def calculate_new_version(latest_tag):
    major, minor, patch = latest_tag.split('.')
    new_minor = int(minor) + 1
    new_version = "{}.{}.0".format(major, str(new_minor))
    print("New version : {}".format(new_version))
    return new_version


def publish_new_version(version):
    print("publishing new version: " + version)
    create_local_tag_cmd = 'git tag -a {0} -m "Release {0}"'.format(version)
    run_cmd(create_local_tag_cmd)
    push_tag_cmd = 'git push origin {0}'.format(version)
    run_cmd(push_tag_cmd)


def should_publish():
    answer = input("Publish the new version? (y/N)")
    if answer == 'y':
        return True
    return False


def create_release():
    update_tags()
    latest_tag = get_latest_tag()
    new_version = calculate_new_version(latest_tag)
    if should_publish():
        publish_new_version(new_version)
    else:
        print("Abort.")


if __name__ == "__main__":
    create_release()
